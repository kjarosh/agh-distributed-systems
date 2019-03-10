#include "tr_utils.h"

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

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
