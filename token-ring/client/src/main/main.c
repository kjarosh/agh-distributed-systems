#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "config.h"

void print_help(char *program);

int main(int argc, char **argv) {
    char *program = argv[0];

    int opt;
    while ((opt = getopt(argc, argv, ":htP:p:n:")) != -1) {
        switch (opt) {
            case 'h':
                print_help(program);
                return 0;

            case 't':
                conf_has_token = true;
                break;

            case 'P':
                if (strcmp("tcp", optarg) == 0) {
                    conf_protocol = TCP;
                } else if (strcmp("udp", optarg) == 0) {
                    conf_protocol = UDP;
                } else {
                    printf("bad protocol: %s\n", optarg);
                    return -1;
                }
                break;

            case 'p':
                conf_port = atoi(optarg);
                break;

            case 'n':
                conf_neighbor_ip = strtok(optarg, ":");
                conf_neighbor_port = atoi(strtok(NULL, ":"));
                break;

            case ':':
                printf("invalid syntax\n");
                return -1;
            case '?':
                printf("unknown option: %c\n", optopt);
                return -1;
        }
    }

    return 0;
}

void print_help(char *program) {
    printf("usage:\n");
    printf("  %s <options>\n", program);
    printf("options:\n");
    printf("  -h\n");
    printf("  -t\n");
    printf("  -P tcp|udp\n");
    printf("  -p <port>\n");
    printf("  -n <neighbor ip>:<neighbor port>\n");
}
