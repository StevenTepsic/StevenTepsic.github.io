# Example Python Code to Insert a Document 
import pymongo

from pymongo import MongoClient 
from bson.objectid import ObjectId 

class AnimalShelter(object): 
    """ CRUD operations for Animal collection in MongoDB """ 

    def __init__(self): 
        # Initializing the MongoClient. This helps to access the MongoDB 
        # databases and collections. This is hard-wired to use the aac 
        # database, the animals collection, and the aac user. 
        # 
        # You must edit the password below for your environment. 
        # 
        # Connection Variables 
        # 
        USER = 'aacuser' 
        PASS = '$3cur3P@$$w0rd' 
        HOST = 'localhost' 
        PORT = 27017 
        DB = 'aac' 
        COL = 'animals' 
        # 
        # Initialize Connection 
        # 
        try:
            self.client = MongoClient(HOST, PORT, username=USER, password=PASS, authSource=DB)
            self.database = self.client['%s' % (DB)] 
            self.collection = self.database['%s' % (COL)]
            # Force a connection check so auth errors are caught here
            self.client.admin.command('ping')
        except pymongo.errors.OperationFailure as e:
            raise Exception(f"Authentication failed: invalid username or password. Details: {e}")
        except pymongo.errors.ConnectionFailure as e:
            raise Exception(f"Could not connect to MongoDB: {e}")
        except Exception as e:
            raise Exception(f"Database connection error: {e}")

    #CRUD create 
    def create(self, data):
        if data is not None: 
            self.database.animals.insert_one(data)  # data should be dictionary 
            return True
        else: 
            raise Exception("Nothing to save, because data parameter is empty") 

    #CRUD read
    def read(self, query=None):
        # Returns all records matching query, or all records if no query given
        if query is None:
            query = {}
        try:
            return list(self.collection.find(query))
        except Exception as e:
            print(f"Read error: {e}")
            return []
                
    #CRUD update
    def update(self, query, update_data, many=False):
        if not query or not update_data:
            raise Exception("parameters required")
        try:
            # update many
            if many:
                result = self.collection.update_many(query, update_data)
            # update one
            else:
                result = self.collection.update_one(query, update_data)
            return result.modified_count
        # error catch
        except Exception as e:
            print(f"Update error: {e}")
            return 0
    
    #CRUD delete
    def delete(self, query, many=False):
        if not query:
            raise Exception("parameter required")
        try:
            # delete many
            if many:
                result = self.collection.delete_many(query)
            # delete one
            else:
                result = self.collection.delete_one(query)
            return result.deleted_count
        # error catch
        except Exception as e:
            print(f"Delete error: {e}")
            return 0