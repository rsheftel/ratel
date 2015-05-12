// This software code is made available "AS IS" without warranties of any        
// kind.  You may copy, display, modify and redistribute the software            
// code either by itself or as incorporated into your code; provided that        
// you do not remove any proprietary notices.  Your use of this software         
// code is at your own risk and you waive any claim against Amazon               
// Digital Services, Inc. or its affiliates with respect to your use of          
// this software code. (c) 2006-2007 Amazon Digital Services, Inc. or its             
// affiliates.          

using System;
using System.Collections;
using System.Net;

namespace Amazon.S3 {
    internal class S3SampleApplication {
        const string awsAccessKeyId = "<INSERT YOUR AWS ACCESS KEY ID HERE>";
        const string awsSecretAccessKey = "<INSERT YOUR AWS SECRET ACCESS KEY HERE>";

        // Convert the bucket name to lowercase for vanity domains.
        // the bucket must be lower case since DNS is case-insensitive.
        static readonly string bucketName = awsAccessKeyId.ToLower() + "-test-bucket";
        const string keyName = "test-key";

        static int Main(string[] args) {
            try {
                if (awsAccessKeyId.StartsWith("<INSERT")) {
                    Console.WriteLine("Please examine S3Driver.cs and update it with your credentials");
                    return 1;
                }

                var conn = new AWSAuthConnection(awsAccessKeyId, awsSecretAccessKey);
                var generator = new QueryStringAuthGenerator(awsAccessKeyId, awsSecretAccessKey);

                // Check if the bucket exists.  The high availability engineering of 
                // Amazon S3 is focused on get, put, list, and delete operations. 
                // Because bucket operations work against a centralized, global
                // resource space, it is not appropriate to make bucket create or
                // delete calls on the high availability code path of your application.
                // It is better to create or delete buckets in a separate initialization
                // or setup routine that you run less often.
                if (conn.checkBucketExists(bucketName)) 
                    Console.WriteLine("----- bucket already exists! -----");
                else {
                    Console.WriteLine("----- creating bucket -----");
                    // to create an EU located bucket change the Location param like this:
                    //  using ( Response response = conn.createBucket( bucketName, Location.EU, null ) )
                    using (var response = conn.createBucket(bucketName, Location.EU, null)) 
                        Console.WriteLine(response.getResponseMessage());
                }

                Console.WriteLine("----- bucket location -----");
                using (var locationResponse = conn.getBucketLocation(bucketName))
                    if (locationResponse.Location == null) Console.WriteLine("Location: <error>");
                    else if (locationResponse.Location.Length == 0) Console.WriteLine("Location: <default>");
                    else Console.WriteLine("Location: '{0}'", locationResponse.Location);

                Console.WriteLine("----- listing bucket -----");
                using (var listBucketResponse = conn.listBucket(bucketName, null, null, 0, null)) dumpBucketListing(listBucketResponse);

                Console.WriteLine("----- putting object -----");
                var obj = new S3Object("This is a test", null);
                var headers = new SortedList {{"Content-Type", "text/plain"}};
                using (var response = conn.put(bucketName, keyName, obj, headers)) Console.WriteLine(response.getResponseMessage());

                Console.WriteLine("----- listing bucket -----");
                using (var listBucketResponse = conn.listBucket(bucketName, null, null, 0, null)) dumpBucketListing(listBucketResponse);

                Console.WriteLine("----- query string auth example -----");
                generator.ExpiresIn = 60 * 1000;

                Console.WriteLine("Try this url in your web browser (it will only work for 60 seconds)\n");
                var url = generator.get(bucketName, keyName, null);
                Console.WriteLine(url);
                Console.Write("\npress enter >");
                Console.ReadLine();

                Console.WriteLine(
                    "\nNow try just the url without the query string arguments.  It should fail.\n");
                Console.WriteLine(generator.makeBaseURL(bucketName, keyName));
                Console.Write("\npress enter >");
                Console.ReadLine();

                Console.WriteLine("----- putting object with metadata and public read acl -----");
                var metadata = new SortedList {{"blah", "foo"}};
                obj = new S3Object("this is a publicly readable test", metadata);

                headers = new SortedList {{"x-amz-acl", "public-read"}, {"Content-Type", "text/plain"}};
                using (var response = conn.put(bucketName, keyName + "-public", obj, headers)) Console.WriteLine(response.getResponseMessage());

                Console.WriteLine("----- anonymous read test -----");
                Console.WriteLine("\nYou should be able to try this in your browser\n");
                var publicURL = generator.get(bucketName, keyName + "-public", null);
                Console.WriteLine(publicURL);
                Console.Write("\npress enter >");
                Console.ReadLine();

                Console.WriteLine("----- path style url example -----");
                Console.WriteLine(
                    "\nNon-location-constrained buckets can also be specified as part of the url path.  (This was the original url style supported by S3.)");
                Console.WriteLine("\nTry this url out in your browser (it will only be valid for 60 seconds)\n");
                generator.CallingFormat = CallingFormat.PATH;
                // could also have been done like this:
                //  generator = new QueryStringAuthGenerator(awsAccessKeyId, awsSecretAccessKey, true, Utils.DEFAULT_HOST, CallingFormat.getPathCallingFormat());
                generator.ExpiresIn = 60 * 1000;
                Console.WriteLine(generator.get(bucketName, keyName, null));
                Console.Write("\npress enter> ");
                Console.ReadLine();

                Console.WriteLine("----- getting object's acl -----");
                using (var response = conn.getACL(bucketName, keyName, null)) Console.WriteLine(response.Object.Data);

                Console.WriteLine("----- deleting objects -----");
                using (var response = conn.delete(bucketName, keyName, null)) Console.WriteLine(response.getResponseMessage());
                using (var response = conn.delete(bucketName, keyName + "-public", null)) Console.WriteLine(response.getResponseMessage());

                Console.WriteLine("----- listing bucket -----");
                using (var listBucketResponse = conn.listBucket(bucketName, null, null, 0, null)) dumpBucketListing(listBucketResponse);

                Console.WriteLine("----- listing all my buckets -----");
                using (var listBucketResponse = conn.listAllMyBuckets(null)) dumpAllMyBucketListing(listBucketResponse);

                Console.WriteLine("----- deleting bucket -----");
                using (var response = conn.deleteBucket(bucketName, null)) Console.WriteLine(response.getResponseMessage());
                return 0;
            } catch (WebException e) {
                Console.WriteLine(e.Status);
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                Console.ReadLine();
                return 1;
            } catch (Exception e) {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                Console.ReadLine();
                return 1;
            }
        }

        static void dumpBucketListing(ListBucketResponse list) {
            foreach (ListEntry entry in list.Entries) {
                var o = entry.Owner ?? new Owner("", "");
                Console.WriteLine(
                    entry.Key.PadRight(20) + entry.ETag.PadRight(20) +
                    entry.LastModified.ToString().PadRight(20) +o.Id.PadRight(10) + 
                    o.DisplayName.PadRight(20) + entry.Size.ToString().PadRight(11) +
                    entry.StorageClass.PadRight(10)
                );
            }
        }

        static void dumpAllMyBucketListing(ListAllMyBucketsResponse list) {
            foreach (Bucket entry in list.Buckets)
                Console.WriteLine(
                    entry.Name.PadRight(20) + entry.CreationDate.ToString().PadRight(20)
                );
        }
    }
}