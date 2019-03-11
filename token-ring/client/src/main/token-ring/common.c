#include "common.h"

const char *tr_logging_address = "224.0.23.182";

char *tr_error = NULL;

pthread_t tr_token_thread;
struct tr_config tr_config;

int tr_server_sock;

int tr_client_sock;

int tr_neighbor_sock;

int tr_running = 1;

struct sockaddr_in tr_neighbor_addr;

int tr_has_token;

uint16_t last_rtc = 0;

uint16_t current_ttl = 64;

uint64_t valid_tid = 0;

tr_queue_t trq_to_pass = TR_QUEUE_INIT;

tr_queue_t trq_to_recv = TR_QUEUE_INIT;

pthread_mutex_t tr_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t trq_to_pass_cond = PTHREAD_COND_INITIALIZER;
pthread_cond_t trq_to_recv_cond = PTHREAD_COND_INITIALIZER;
