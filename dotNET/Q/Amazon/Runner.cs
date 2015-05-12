using amazon;
using Q.Util;

namespace Q.Amazon {
    public class Runner : Objects {
        public static void Main(string[] args) {
            var userData = EC2Runner.userData();
            var q = new SqsQ((string) userData.get("requestQueue"));
            while(trueDat()) {
                var messages = q.messagesBlocking();
                each<Message>(messages, message => {
                    var request = (TestEC2.TestRequest) message.@object();
                    request.sendResponse();
                    message.delete();
                });
            }
        }

    }
}
