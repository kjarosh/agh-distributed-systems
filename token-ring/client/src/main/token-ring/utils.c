#include "utils.h"

#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>

void tr_log(const char *message) {
    printf("[TR] %s\n", message);
}

uint64_t tr_random_tid() {
    uint64_t ret;
    int fd;
    if ((fd = open("/dev/random", O_RDONLY)) != 0) {
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
