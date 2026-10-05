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
        # Fixed a bug here. This used to write straight to
        # self.database.animals instead of self.collection. It only worked
        # because the collection is always named animals anyway. Using
        # self.collection is what the rest of the class actually uses.
        if data is None:
            raise Exception("Nothing to save, because data parameter is empty")
        # Added a type check. Anything other than a dictionary should fail
        # here with a clear message instead of confusing MongoDB later.
        if not isinstance(data, dict):
            raise TypeError("data must be a dictionary")
        self.collection.insert_one(data)
        return True

    #CRUD read
    def read(self, query=None):
        # Returns all records matching query, or all records if no query given
        if query is None:
            query = {}
        # Same idea as create. If query isn't a dictionary, say so now
        # instead of letting it fail somewhere inside find().
        if not isinstance(query, dict):
            raise TypeError("query must be a dictionary")
        try:
            return list(self.collection.find(query))
        except Exception as e:
            print(f"Read error: {e}")
            return []

    #CRUD update
    def update(self, query, update_data, many=False):
        # Switched this from a truthiness check to a type check. An empty
        # dictionary is a valid "match everything" query in MongoDB, but
        # the old check (not query) would have rejected it anyway.
        if not isinstance(query, dict) or not isinstance(update_data, dict):
            raise TypeError("query and update_data must both be dictionaries")
        if not update_data:
            raise Exception("update_data must not be empty")
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
        # Same fix as update. Checking the type instead of just truthiness.
        if not isinstance(query, dict):
            raise TypeError("query must be a dictionary")
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

    #CRUD aggregate
    def aggregate(self, pipeline):
        # New method. read() only supports find(), so there was no way to
        # run an aggregation pipeline before this. The dashboard needs this
        # for the breed count chart, so MongoDB can do the counting instead
        # of pulling every row back and counting it in pandas.
        if not isinstance(pipeline, list):
            raise TypeError("pipeline must be a list of stages")
        try:
            return list(self.collection.aggregate(pipeline))
        except Exception as e:
            print(f"Aggregate error: {e}")
            return []