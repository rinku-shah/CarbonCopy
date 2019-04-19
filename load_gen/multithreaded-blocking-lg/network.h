#ifndef UTILS_H
#define UTILS_H
#include <fcntl.h>
#include "utils.h"
#endif

struct get_t{
	uint8_t msg_id;
    uint32_t key;
    uint32_t val;
    uint8_t ver; 
};

struct put_t{
	uint8_t msg_id;
    uint32_t key;
    uint32_t val;
    uint8_t ver; 
};

struct writeReply_t{
	uint8_t msg_id;
    uint32_t key;
    uint32_t val;
    uint8_t ver; 
};

struct readReply_t{
	uint8_t msg_id;
    uint32_t key;
    uint32_t val;
    uint8_t ver; 
};


class Network{
public:
	int tID;
	int client_socket;
    char client_buffer[BUFFER_SIZE];
    unsigned char my_client_byte_buffer[BUFFER_SIZE];
    char write_client_buffer[BUFFER_SIZE];
	char write_client_byte_buffer[BUFFER_SIZE];
	int sock_raw; //To receive raw packets
	int saddr_size , data_size;
	struct sockaddr saddr;
	// Byte array in C++
	unsigned char client_byte_buffer[BUFFER_SIZE];	

	int server_port;
	const char *server_address;
	struct sockaddr_in server_sock_addr;
	struct sockaddr_in source,dest;
	//bool flag=false; //flag to test the right dest IP for rcv data 
	bool tflag=false; //flag to test dest UDP port for demux of packets
	time_t cT;
	time_t eT;
    double timeout = 2;
    bool timeoutFlag=false; // Flag =true if read timedout

	// Constructor
	Network(int);

	// Socket methods
	void input_server_details(int,const char*);
	void read_data();
	void read_data2();
	void write_data(string);
	void write_data2(int);
	void read_byte();
	void write_byte();

	int sendUEData(int, string, string, int, int, int, string, size_t);

	// Utility functions
	string GetStdoutFromCommand(string cmd);
	string runIperfCommand(string cmd,string srcIp);	

	//Raw packet functions

	int ProcessPacket(unsigned char* , int);
	int ProcessPacket2(unsigned char* , int);
	void print_ip_header(unsigned char* , int);
	void print_ip_header2(unsigned char* , int);
	void print_udp_packet(unsigned char * , int );
	void print_udp_packet2(unsigned char * , int );

	// Destructor
	~Network();		
};
