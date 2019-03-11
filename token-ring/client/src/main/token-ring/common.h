#ifndef TOKEN_RING_TR_COMMON_H
#define TOKEN_RING_TR_COMMON_H

#include <pthread.h>
#include <stdint.h>
#include <arpa/inet.h>
#include "queue.h"
#include "packet_formats.h"

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
    int join;
};

extern pthread_t tr_token_thread;
extern pthread_t tr_aux_thread;
extern struct tr_config tr_config;

/// server sock, listening for clients
extern int tr_server_sock;

/// client connected to the server
extern int tr_client_sock;

/// neighbor socket
extern int tr_neighbor_sock;

extern int tr_running;

extern struct sockaddr_in tr_neighbor_addr;

/**
 * This value is true if this machine currently owns the token,
 * false otherwise.
 */
extern int tr_has_token;

/**
 * Last round trip counter, read from the token,
 * if no information is available this value should be 0.
 */
extern uint16_t last_rtc;

/**
 * TTL currently in use, upon receiving the token
 * this value should be recalculated.
 */
extern uint16_t current_ttl;

/**
 * Current valid token ID, if any token with ID less than this
 * value is encountered, it's considered phony.
 */
extern uint64_t valid_tid;

/**
 * Queue of packets to pass to the neighbor.
 */
extern tr_queue_t trq_to_pass;

/**
 * Queue of packets meant to be delivered to this machine.
 */
extern tr_queue_t trq_to_recv;

extern pthread_mutex_t tr_mutex;
extern pthread_cond_t trq_to_pass_cond;
extern pthread_cond_t trq_to_recv_cond;

#endif
