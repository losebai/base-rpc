import socket  
import time


def server(host,port):
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # 绑定端口号
    server_socket.bind((host, port))

    # 等待客户端连接
    server_socket.listen(5)
    print('等待客户端连接...')

    while True:
        # 建立客户端连接
        client_socket, addr = server_socket.accept()
        
        print('连接地址：', addr)
        
        # 向客户端发送消息
        message = '欢迎访问菜鸟教程！' + "\r\n"
        client_socket.send(message.encode('utf-8'))
        
        # 关闭连接
        client_socket.close()

def client(host,port):
    # 创建socket对象
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # 连接服务，指定主机和端口
    client_socket.connect((host, port))

    message = '欢迎访问菜鸟教程！' + "\r\n"
    client_socket.send(message.encode('utf-8'))
    # time.sleep(2)

    # 接收小于1024字节的数据
    message = client_socket.recv(1024)
    
    client_socket.close()

    print(message)
    print(message.decode('utf-8'))


if __name__ == "__main__":
    host = 'localhost'
    port = 7777
    client(host=host,port=port)


