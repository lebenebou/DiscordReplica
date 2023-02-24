
#include <iostream>
#include <string.h>
#include <string>

// only for linux
#include <netdb.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>

using namespace std;

int main(int argc, char const *argv[]){

    int listening = socket(AF_INET, SOCK_STREAM, 0);
    if(listening < 0){
        cout << "Error when creating socket" << endl;
        return -1;
    }
    cout << "Created socket" << endl;

    const int port = 54000;

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

    sockaddr_in client;
    socklen_t clientSize = sizeof(client);
    char host[NI_MAXHOST];
    char svc[NI_MAXSERV];

    int clientSocket = accept(listening, (struct sockaddr*)&client, &clientSize);

    if(clientSocket == -1){

        cout << "Couldn't connect with client" << endl;
        return -4;
    }
    cout << "Connected to client" << endl;

    close(listening);

    memset(host, 0, NI_MAXHOST); // clear array
    memset(svc, 0, NI_MAXSERV); // clear array

    int result = getnameinfo((struct sockaddr*)&client, sizeof(client), host, NI_MAXHOST, svc, NI_MAXSERV, 0);
    if(result > 0){
        cout << host << " connected on " << svc << endl;
    }
    else{ // result == 0
        inet_ntop(AF_INET, &client.sin_addr, host, NI_MAXHOST);
        cout << host << " connected on " << ntohs(client.sin_port) << endl;
    }
    cout << endl;
    
    // Connection established
    const size_t buffer_size = 4096;
    char buffer[buffer_size];
    string received_message;

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
        
        received_message = string(buffer, 0, bytesReceived);
        cout << "Received: " << received_message << endl;
        send(clientSocket, buffer, bytesReceived+1, 0); // send back to client
    }

    close(clientSocket);
    return 0;
}