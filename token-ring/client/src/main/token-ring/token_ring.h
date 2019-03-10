#ifndef TOKEN_RING_TOKEN_RING_H
#define TOKEN_RING_TOKEN_RING_H

#include <stdint.h>

#include "init.h"

int tr_send(const void *buf, size_t len, const tr_identifier *to);

int tr_recv(void *buf, size_t len, int flags, tr_identifier *from);

#endif
