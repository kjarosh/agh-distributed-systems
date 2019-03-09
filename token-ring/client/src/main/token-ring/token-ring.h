#ifndef TOKEN_RING_TOKEN_RING_H
#define TOKEN_RING_TOKEN_RING_H

#include <stdlib.h>
#include <stdbool.h>

const char *tr_logging_address = "224.0.23.182";

enum tr_flags {
    TR_DONTWAIT = 1,
    TR_WAITFORSEND = 2
};

enum tr_protocol_t {
    TR_UDP, TR_TCP
};

struct tr_config {
    enum tr_protocol_t proto;
    int port;
    char neighbor_host[256];
    int neighbor_port;
};

extern char *tr_error;

int tr_init(const struct tr_config *conf, int has_token);

int tr_send(const void *buf, size_t len, int flags);

int tr_recv(void *buf, size_t len, int flags);

#endif
