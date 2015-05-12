using System.IO;
using System.Net.Mail;

namespace TradingScreenApiService.Email
{
    public static class Emailer
    {
        const string Server = "mail.fftw.com";
        const int Port = 25;
        public static string FromAddress { get; set; }
        public static string ToAddress { get; set; }

        static Emailer() {
            FromAddress = "Trading Screen API<alert@malbecpartners.com>";
            ToAddress = "Michael Franz<mfranz@fftw.com>";
        }

        static SmtpClient PrepareClient()
        {
            var client = new SmtpClient(Server, Port);

            return client;
        }

        static MailMessage PrepareMailMessage(string subject, string body)
        {
            var message = new MailMessage(FromAddress, ToAddress, subject, body);
            return message;
        }

        public static void Send(string subject, string body)
        {
            PrepareClient().Send(PrepareMailMessage(subject, body));
        }

        public static void Send(string subject, string body, FileInfo[] attachments)
        {
            var msg = PrepareMailMessage(subject, body);
            var copyOfFiles = (FileInfo[])attachments.Clone();

            foreach (var fi in copyOfFiles) {
                if (!File.Exists(fi.FullName)) continue;

                var attach = new Attachment(fi.FullName);
                msg.Attachments.Add(attach);
            }

            PrepareClient().Send(msg);
        }
    }
}
