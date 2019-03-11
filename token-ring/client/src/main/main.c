#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>

#include "token-ring/token_ring.h"

void print_help(char *program) {
    printf("usage:\n");
    printf("  %s <options>\n", program);
    printf("options:\n");
    printf("  -h  (help)\n");
    printf("  -t  (i have the token)\n");
    printf("  -j  (join to a token ring)\n");
    printf("  -i <identifier>\n");
    printf("  -P tcp|udp\n");
    printf("  -p <port>\n");
    printf("  -n <neighbor ipv4>:<neighbor port>\n");
}

int main(int argc, char **argv) {
    srand(time(NULL));
    char *program = argv[0];
    int has_token = 0;
    struct tr_config config;
    config.join = 0;

    int opt;
    while ((opt = getopt(argc, argv, ":htP:p:n:i:")) != -1) {
        switch (opt) {
            case 'h':
                print_help(program);
                return 0;

            case 't':
                has_token = 1;
                break;

            case 'P':
                if (strcmp("tcp", optarg) == 0) {
                    config.proto = TR_TCP;
                } else if (strcmp("udp", optarg) == 0) {
                    config.proto = TR_UDP;
                } else {
                    printf("bad protocol: %s\n", optarg);
                    return -1;
                }
                break;

            case 'p':
                config.port = (uint16_t) atoi(optarg);
                break;

            case 'n':;
                char *host = strtok(optarg, ":");
                strncpy(config.neighbor_ip, host, 256);
                config.neighbor_port = (uint16_t) atoi(strtok(NULL, ":"));
                break;

            case 'i':
                strncpy(config.identifier, optarg, 256);
                break;

            case 'j':
                config.join = 1;
                break;

            case ':':
                printf("invalid syntax\n");
                return -1;
            case '?':
                printf("unknown option: %c\n", optopt);
                return -1;
        }
    }

    if (tr_init(&config, has_token) != 0) {
        tr_perror();
        return -1;
    }

    char *message = "asdf";
    tr_identifier to = "K";
    if (tr_send(message, 4, &to) != 0) {
        tr_perror();
    }

    char buf[128];
    if (tr_recv(&buf[0], 128, 0, NULL) != 0) {
        tr_perror();
    }
    printf("%s\n", &buf[0]);

    return 0;
}
