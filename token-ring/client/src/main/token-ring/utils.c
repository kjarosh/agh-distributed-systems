#include "utils.h"
#include "packet_formats.h"
#include "common.h"

#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

void tr_perror() {
    perror(tr_error);
}

void tr_log(const char *message) {
    printf("[TR] %s\n", message);
    tr_logger_send(message);
}

uint64_t tr_random_tid() {
    uint64_t ret;
    int fd;
    if ((fd = open("/dev/urandom", O_RDONLY)) != 0) {
        return (uint64_t) rand();
    }

    if (read(fd, &ret, sizeof(ret)) < 0) {
        return (uint64_t) rand();
    }

    close(fd);
    return ret;
}

struct timespec tr_get_now() {
    struct timespec ret;
    struct timeval now;
    gettimeofday(&now, NULL);
    TIMEVAL_TO_TIMESPEC(&now, &ret);
    return ret;
}

struct timespec tr_calc_wakeup_point() {
    struct timespec wakeup_point = tr_get_now();
    wakeup_point.tv_sec += 1;
    return wakeup_point;
}

const char *tr_logging_address = "224.0.23.182";
int tr_logger_sock = -1;

void tr_logger_send(const char *message) {
    if (tr_logger_sock == -1) {
        if ((tr_logger_sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
            printf("[TR] failed to create logging socket\n");
            return;
        }
    }

    struct sockaddr_in logging_addr;
    memset(&logging_addr, 0, sizeof(logging_addr));

    logging_addr.sin_family = AF_INET;
    logging_addr.sin_addr.s_addr = inet_addr(tr_logging_address);
    logging_addr.sin_port = htons(1440);

    struct tr_packet_log packet;
    strncpy(packet.sender, tr_config.identifier, 256);
    strncpy(packet.message, message, 512);
    packet.timestamp = (uint64_t) time(NULL);

    if (sendto(tr_logger_sock,
               &packet, sizeof(struct tr_packet_log), 0,
               (struct sockaddr *) &logging_addr, sizeof(logging_addr)) <= 0) {
        printf("[TR] failed to send log message\n");
    }
}
