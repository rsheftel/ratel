using System;

namespace SqsPong {
    class Program {

        static void Main(string[] args) {
            if (args.Length != 0) Console.WriteLine("no args necessary");
            var awsId = "1VA799Q6986T9ZDX8DG2";
            var secret = "gD0NUGToPDv7MzTEi3G4EbYJ1gH1tEq+NBwLxxXE";
            var s3 = new AWSAuthConnection(awsId, secret, false, CallingFormat.PATH);


            var bucket = "knell-Q17Test";
            s3.createBucket(bucket, null, null);
            //var builder = new StringBuilder();
            //for (var i = 0; i < 2000; i++) builder.Append("2008-01-23 04:56:07,9876.5432109876\n");
            //var dataString = builder.ToString();
            var start = DateTime.Now;
            using (var response = s3.listBucket(bucket, null, null, 100, null)) {
                foreach (var entry in response.Entries) {
                    using (s3.delete(bucket, ((ListEntry) entry).Key, null)) {}
                    Console.WriteLine("deleted " + entry);
                }
            }
            using (s3.deleteBucket(bucket, null)) {}
            Console.WriteLine("s3 retrieved in " + DateTime.Now.Subtract(start).TotalSeconds);
        }
    }
}
