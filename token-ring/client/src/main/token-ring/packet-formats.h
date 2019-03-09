#ifndef TOKEN_RING_PACKET_FORMATS_H
#define TOKEN_RING_PACKET_FORMATS_H

struct tr_packet_log {
    uint8_t sender_id;
    uint64_t timestamp;
    char message[128];
};

#endif
