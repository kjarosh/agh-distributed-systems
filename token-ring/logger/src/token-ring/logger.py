import socket
import struct

tr_logging_address = '224.0.23.182'
tr_logging_port = 1440
tr_packet_log_format = (
        'B' +  # sender id
        'Q' +  # timestamp
        '128s')  # log message


def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind((tr_logging_address, tr_logging_port))

    while True:
        buff, address = server_socket.recvfrom(256)
        print("received msg: " + str(buff, 'utf-8'))
        log_message = struct.unpack(tr_packet_log_format, buff)
        print(str(log_message[0]) + ' ' + str(log_message[1]) + ' ' + str(log_message[2]))


if __name__ == '__main__':
    main()
