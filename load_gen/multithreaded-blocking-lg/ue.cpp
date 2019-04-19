/********************************************************************
 * This file contains all the functionalities associated with a UE. *
 ********************************************************************/

#include "ue.h"
#include <time.h>

char SEPARATOR[] = "@:##:@";

string READ = "6"; //get
string READ_REPLY = "1";  	
string WRITE = "2"; //put
string WRITE_REPLY = "3";

vector<unsigned char> intToBytes(int paramInt)
{
     vector<unsigned char> arrayOfByte(4);
     for (int i = 0; i < 4; i++)
         arrayOfByte[3 - i] = (paramInt >> (i * 8));
     return arrayOfByte;
}

/*
 * Constructor: Create a UE object.
 */
UserEquipment::UserEquipment(int ue_num){
	
}

void UserEquipment::get(Network &user, int key){ //READ
	string send, receive;
	vector<string> tmpArray;
	time_t curTime;
	time(&curTime);
    struct get_t g;

    g.msg_id = 6; //READ
    g.key = htonl(key);
    g.val = htonl(0x00000080);
    g.ver = 0;      
	
    do{
    	bzero(user.client_buffer, BUFFER_SIZE);
		int len=0;

		memcpy(user.client_buffer, &(g.msg_id), sizeof(g.msg_id));
		len+=sizeof(g.msg_id);

    	memcpy(user.client_buffer+len, &(g.key), sizeof(g.key));
		len+=sizeof(g.key);
	
		memcpy(user.client_buffer+len, &(g.val), sizeof(g.val));
		len+=sizeof(g.val);

		memcpy(user.client_buffer+len, &(g.ver), sizeof(g.ver));
		len+=sizeof(g.ver);

		user.write_data2(len);

		time(&curTime);
	
		user.read_data();
        } while(user.timeoutFlag);

    time(&curTime);
	receive = (string) (user.client_buffer);
	if(MY_DEBUG){
	cout<<"My Print -- received"<<receive<<endl;
	}
    char str[4], v[8];
    strncpy(str, user.client_buffer, 1);
    str[1] = '\0';   /* null character manually added */
    strncpy(v, user.client_buffer+5, 4);
    v[5] = '\0';
	if(str == READ_REPLY){
			if(DO_DEBUG){
				cout <<"READ REPLY VALUE = "<< v <<endl;
			}
     }

}

void UserEquipment::put(Network &user, int key, int val){ //WRITE
	string send, receive;
	vector<string> tmpArray;
	time_t curTime;
	time(&curTime);
    struct put_t g;

    g.msg_id = 2; //WRITE
    g.key = htonl(key);
    g.val = htonl(val);
    g.ver = 0;      
	
    do{
    	bzero(user.client_buffer, BUFFER_SIZE);
		int len=0;

		memcpy(user.client_buffer, &(g.msg_id), sizeof(g.msg_id));
		len+=sizeof(g.msg_id);

    	memcpy(user.client_buffer+len, &(g.key), sizeof(g.key));
		len+=sizeof(g.key);
	
		memcpy(user.client_buffer+len, &(g.val), sizeof(g.val));
		len+=sizeof(g.val);

		memcpy(user.client_buffer+len, &(g.ver), sizeof(g.ver));
		len+=sizeof(g.ver);

		user.write_data2(len);

		time(&curTime);
	
		user.read_data();
        } while(user.timeoutFlag);

    time(&curTime);
	receive = (string) (user.client_buffer);
	if(MY_DEBUG){
	cout<<"My Print -- received"<<receive<<endl;
	}
    char str[4], v[8];
    strncpy(str, user.client_buffer, 1);
    str[1] = '\0';   /* null character manually added */
    strncpy(v, user.client_buffer+5, 4);
    v[5] = '\0';
	if(str == WRITE_REPLY){
			if(DO_DEBUG){
				cout <<"READ REPLY VALUE = "<< v <<endl;
			}
     }

}

UserEquipment::~UserEquipment(){
	// Dummy destructor
}
