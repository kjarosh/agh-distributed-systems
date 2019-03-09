#include "tr_queue.h"

#include <stdlib.h>

int tr_queue_empty(tr_queue_t *q) {
    return q->first == NULL;
}

struct tr_packet_data *tr_queue_get(tr_queue_t *q) {
    struct tr_queue_node_t *next_first = q->first->next;
    struct tr_packet_data *ret = q->first->value;

    free(q->first);
    q->first = next_first;
    if (next_first == NULL) {
        q->last = NULL;
    }

    return ret;
}

void tr_queue_put(tr_queue_t *q, struct tr_packet_data *packet) {

}
