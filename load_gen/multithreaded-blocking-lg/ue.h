#include "network.h"

class UserEquipment{
	public:
		
		// Constructor
		UserEquipment(int);

		/* Functions  */
		void get(Network&, int); //read
		void put(Network&, int, int); //write
		
		// Destructor
		~UserEquipment();		
};
