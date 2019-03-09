#ifndef TOKEN_RING_CONFIG_H
#define TOKEN_RING_CONFIG_H

#include <stdbool.h>

enum conf_protocol_t {
    UDP, TCP
};

extern bool conf_has_token;
extern enum conf_protocol_t conf_protocol;
extern int conf_port;

extern char *conf_neighbor_ip;
extern int conf_neighbor_port;

#endif
