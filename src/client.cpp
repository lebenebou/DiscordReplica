
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
    cout << "Successfully connected to Server." << endl;

    char buf[4096];
    string userInput;
    bool first_message = true;
    string username = "Client";

    while(true){

        if(first_message) cout << "Your username: ";
        else cout << "Type a message> ";

        getline(cin, userInput); // get input from user

        if(userInput == "/quit"){
            close(clientSocket); // disconnect
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
            cout << "Couldn't get a response back from the server" << endl;
        }
        else if(first_message){
            cout << string(buf, 0, bytesReceived) << endl; // display the servers response
            first_message = false;
        }
    }

    close(clientSocket); // disconnect

    return 0;
}