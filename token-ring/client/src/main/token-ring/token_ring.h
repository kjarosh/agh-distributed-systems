#ifndef TOKEN_RING_TOKEN_RING_H
#define TOKEN_RING_TOKEN_RING_H

#include <stdlib.h>
#include <stdbool.h>

#include "packet_formats.h"

extern const char *tr_logging_address;

enum tr_flags {
    TR_DONTWAIT = 1
};

enum tr_protocol_t {
    TR_UDP, TR_TCP
};

struct tr_config {
    tr_identifier identifier;
    enum tr_protocol_t proto;
    uint16_t port;
    char neighbor_ip[256];
    uint16_t neighbor_port;
};

extern char *tr_error;

int tr_init(const struct tr_config *conf, int has_token);

int tr_send(const void *buf, size_t len, const tr_identifier *to);

int tr_recv(void *buf, size_t len, int flags, tr_identifier *from);

#endif
