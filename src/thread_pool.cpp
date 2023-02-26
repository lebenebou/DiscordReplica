
#include <thread>
#include <queue>
#include <mutex>
#include <condition_variable>
#include <functional>
using namespace std;

class ThreadPool {

private:
    int num_threads;
    std::vector<std::thread> threads;
    std::queue<std::function<void()>> tasks;
    std::mutex mutex;
    std::condition_variable condition;
    bool stop;

public:
    ThreadPool(const int& num_threads) : num_threads(num_threads), stop(false) {

        for (int i = 0; i < num_threads; i++) {
            threads.emplace_back([this]() {
                while (true) {
                    std::unique_lock<std::mutex> lock(mutex);
                    condition.wait(lock, [this]() { return !tasks.empty() || stop; });
                    if (stop && tasks.empty()) {
                        return;
                    }
                    auto task = std::move(tasks.front());
                    tasks.pop();
                    lock.unlock();
                    task();
                }
            });
        }
    }

    ~ThreadPool() {
        {
            std::unique_lock<std::mutex> lock(mutex);
            stop = true;
        }
        condition.notify_all();
        for (auto& thread : threads) {
            thread.join();
        }
    }

    template <typename F>
    void enqueue(F&& task) {
        {
            std::unique_lock<std::mutex> lock(mutex);
            tasks.emplace(std::forward<F>(task));
        }
        condition.notify_one();
    }
};