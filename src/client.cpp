
#include <iostream>
#include <sys/types.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <string.h>
#include <string>

using namespace std;

int main(int argc, char const *argv[])
{
    int clientSocket= socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket== -1){
        cout << "Couldn't create socket" << endl;
        return 1;
    }

    const int port = 54000;
    string ipAddress = "127.0.0.1";

    sockaddr_in hint;
    hint.sin_family = AF_INET;
    hint.sin_port = htons(port);
    inet_pton(AF_INET, ipAddress.c_str(), &hint.sin_addr);

    int result = connect(clientSocket, (sockaddr*)&hint, sizeof(hint));
    if (result == -1){
        cout << "Couldn't connect to server. Server might not be online." << endl;
        return 1;
    }

    char buf[4096];
    string userInput;

    while(true){

        cout << "Type a message> ";
        getline(cin, userInput); // get input from user
        if(userInput == "/quit"){
            close(clientSocket);
            return 0;
        }

        int result = send(clientSocket, userInput.c_str(), userInput.size() + 1, 0);
        if (result == -1){
            cout << "Could not send to server.";
            continue;
        }

        memset(buf, 0, 4096); // clear buffer array
        int bytesReceived = recv(clientSocket, buf, 4096, 0); // receive bytes from the server

        if (bytesReceived == -1){
            cout << "Couldn't get response back from server" << endl;
        }
        else{
            cout << "SERVER> " << string(buf, 0, bytesReceived) << endl;
        }
    }

    close(clientSocket); // disconnect

    return 0;
}