#ifndef TOKEN_RING_PACKET_FORMATS_H
#define TOKEN_RING_PACKET_FORMATS_H

#include <stdint.h>
#include "queue.h"

typedef char tr_identifier[256];

enum tr_packet_type {
    TRP_TOKEN = 0,
    TRP_TOKEN_ACK = 1,
    TRP_DATA = 2,
    TRP_SWITCH = 3,
    TRP_RESET = 4
};

struct tr_packet_token {
    uint8_t type;
    uint64_t tid; // token ID
    uint16_t rtc; // round trip counter
};

struct tr_packet_token_ack {
    uint8_t type;
};

struct tr_packet_data {
    uint8_t type;
    uint16_t ttl;
    tr_identifier sender;
    tr_identifier recipient;
    uint16_t data_length;
    char data[0];
};

/**
 * This packets instructs the recipient to switch to
 * the given neighbor.
 */
struct tr_packet_switch {
    uint8_t type;
    char neighbor_ip[256];
    uint16_t neighbor_port;
};

/**
 * This packets instructs the recipient to reset all
 * established connections with clients.
 */
struct tr_packet_reset {
    uint8_t type;
};

struct tr_packet_log{
    tr_identifier sender;
    uint64_t timestamp;
    char message[512];
};

#endif
