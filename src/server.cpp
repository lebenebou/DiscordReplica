
#include <iostream>
#include <string.h>
#include <string>

// only for linux
#include <netdb.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>

#include <vector>
#include <thread>
#include <atomic>
#include <set>
#include <mutex>

#include "thread_pool.cpp"

using namespace std;

set<int> connectedClients; // set containing the sockets of connected clients

void handleClient(int clientSocket, const sockaddr_in& clientAddr) {
    
    char host[NI_MAXHOST];
    char svc[NI_MAXSERV];

    memset(host, 0, NI_MAXHOST); // clear array
    memset(svc, 0, NI_MAXSERV); // clear array

    int result = getnameinfo((struct sockaddr*)&clientAddr, sizeof(clientAddr), host, NI_MAXHOST, svc, NI_MAXSERV, 0);
    if(result > 0){
        cout << host << " connected on " << svc << endl;
    }
    else{ // result == 0
        inet_ntop(AF_INET, &clientAddr.sin_addr, host, NI_MAXHOST);
        cout << host << " connected on " << ntohs(clientAddr.sin_port) << endl;
    }
    cout << endl;

    const short buffer_size = 4096;
    char buffer[buffer_size];

    while(true){

        memset(buffer, 0, buffer_size); // clear buffer array
        int bytesReceived = recv(clientSocket, buffer, buffer_size, 0);

        if(bytesReceived == -1){
            cout << "Connection issue: couldn't receive bytes" << endl;
            break;
        }
        else if(bytesReceived == 0){
            cout << "Client diconnected" << endl;
            break;
        }

        string received_message = string(buffer, 0, bytesReceived);
        cout << "Message received: " << received_message << endl;
        send(clientSocket, buffer, bytesReceived+1, 0); // echo message back to the client
    }

    // client was disconnected
    connectedClients.erase(clientSocket);
    close(clientSocket);
}

int main(int argc, char const *argv[]){

    if(argc != 2){
        cout << "Must run file with one argument: <PortNumber>" << endl;
        cout << "Syntax example: g++ server.cpp -o runnable.exe && ./runnable.exe 8080" << endl;
        return 1;
    }

    int listening = socket(AF_INET, SOCK_STREAM, 0);
    if(listening < 0){
        cout << "Error when creating socket" << endl;
        return -1;
    }
    cout << "Created socket" << endl;

    int port = stoi(argv[1]);
    cout << "Attempting to run on port " << port << endl;

    sockaddr_in hint;
    hint.sin_family = AF_INET;
    hint.sin_port = htons(port);
    inet_pton(AF_INET, "0.0.0.0", &hint.sin_addr); // 0.0.0.0 is to get any address

    if(bind(listening, (struct sockaddr*)&hint, sizeof(hint))){
        cout << "Couldn't bind to IP/port" << endl;
        return -2;
    }
    cout << "Binded IP/Port" << endl;

    if(listen(listening, SOMAXCONN) == -1){
        cout << "Couldn't listen" << endl;
        return -3;
    }

    cout << "Listening..." << endl;
    ThreadPool pool(128);

    while(true){ // keep listening for incoming connections

        sockaddr_in client;
        socklen_t clientSize = sizeof(client);
        
        int clientSocket = accept(listening, (struct sockaddr*)&client, &clientSize);

        if(clientSocket == -1){

            cout << "Couldn't connect to client" << endl;
            return -4;
        }
        cout << "A client just connected" << endl;
        
        connectedClients.insert(clientSocket);
        pool.enqueue( [clientSocket, client]() -> void { handleClient(clientSocket, client); });
    }

    close(listening);
    return 0;
}