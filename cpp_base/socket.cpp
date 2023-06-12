#ifdef _WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <arpa/inet.h>
#endif

#include <stdio.h>
#include <string.h>

int main()
{
#ifdef _WIN32
    WSADATA wsaData;
    int result = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if (result != 0)
    {
        printf("WSAStartup failed: %d\n", result);
        return 1;
    }
#endif

    // 创建 Socket
    int serverSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (serverSock < 0)
    {
        printf("socket error");
        return 1;
    }

    // 绑定 IP 地址和端口号
    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(7777);
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    int result = bind(serverSock, (struct sockaddr *)&addr, sizeof(addr));
    if (result < 0)
    {
        printf("bind error");
        return 1;
    }

    // 监听 Socket
    result = listen(serverSock, SOMAXCONN);
    if (result < 0)
    {
        printf("listen error");
        return 1;
    }

    // 接受连接请求
    struct sockaddr_in clientAddr;
    socklen_t addrLen = sizeof(clientAddr);
    int clientSock = accept(serverSock, (struct sockaddr *)&clientAddr, &addrLen);
    if (clientSock < 0)
    {
        printf("accept error");
        return 1;
    }
    char* data = "asda\0";
    send(clientSock,data,sizeof(data),sizeof(data));

    // 接收和发送数据
    char buf[1024];
    while (1)
    {
        int len = recv(clientSock, buf, sizeof(buf), 0);
        if (len < 0)
        {
            printf("recv error");
            break;
        }
        if (len == 0)
        {
            printf("client closed");
            break;
        }

        len = send(clientSock, buf, len, 0);
        if (len < 0)
        {
            printf("send error");
            break;
        }
    }

#ifdef _WIN32
    closesocket(clientSock);
    closesocket(serverSock);
    WSACleanup();
#else
    close(clientSock);
    close(serverSock);
#endif

    return 0;
}