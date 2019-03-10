#ifndef TOKEN_RING_TR_UTILS_H
#define TOKEN_RING_TR_UTILS_H

#include <stdint.h>
#include <sys/time.h>

extern char *tr_error;

void tr_perror();

void tr_log(const char *message);

uint64_t tr_random_tid();

struct timespec tr_get_now();

struct timespec tr_calc_wakeup_point();

#endif
