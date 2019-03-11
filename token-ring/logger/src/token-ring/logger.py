from datetime import datetime
import socket
import struct

tr_logging_address = '224.0.23.182'
tr_logging_port = 1440
tr_packet_log_format = (
        '256s' +  # sender id
        'Q' +  # timestamp
        '512s')  # log message


def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind((tr_logging_address, tr_logging_port))

    membership_request =\
        struct.pack("@4sl", socket.inet_aton(tr_logging_address), socket.INADDR_ANY)
    server_socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, membership_request)

    while True:
        buff, address = server_socket.recvfrom(776)
        sender, timestamp, message = struct.unpack(tr_packet_log_format, buff)

        sender = sender.decode().split('\0', 1)[0]
        time = datetime.utcfromtimestamp(timestamp).strftime('%H:%M:%S')
        message = message.decode().split('\0', 1)[0]

        print(sender + ' ' + time + ' ' + message)


if __name__ == '__main__':
    main()
