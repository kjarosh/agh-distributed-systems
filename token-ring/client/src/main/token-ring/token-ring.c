#include "token-ring.h"

#include <pthread.h>

char *tr_error = NULL;

pthread_t tr_thread;
struct tr_config tr_config;
int tr_has_token;

void *tr_thread_main(void *arg) {

    return NULL;
}

int tr_init(const struct tr_config *conf, int has_token) {
    tr_has_token = has_token;
    tr_config = *conf;

    if (pthread_create(&tr_thread, NULL, tr_thread_main, NULL)) {
        tr_error = "Error creating thread";
        return 1;
    }

    return 0;
}

int tr_send(const void *buf, size_t len, int flags) {
    return 0;
}

int tr_recv(void *buf, size_t len, int flags) {
    return 0;
}
