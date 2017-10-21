using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Rover2Server
{
    public class Commander
    {

        public static IList<WebSocket> sockets = new List<WebSocket>();

        private static byte[] _image;

        public static void SetImage(byte[] image)
        {
            _image = image;
        }

        public static byte[] GetImage()
        {
            return _image;
        }

        public static async Task Echo(HttpContext context, WebSocket webSocket)
        {
            sockets.Add(webSocket);
            CancellationToken ct = context.RequestAborted;
            while (webSocket.State == WebSocketState.Open)
            {
                string mystring = await ReceiveStringAsync(webSocket);
                if (webSocket.State == WebSocketState.Open)
                {
                    await SendStringAsync(webSocket, mystring, ct);
                }
            }
            await webSocket.CloseAsync(WebSocketCloseStatus.NormalClosure,"Closing", ct);
            webSocket.Dispose();
            sockets.Remove(webSocket);
        }

        public static void SendString(string data)
        {
            foreach(var x in sockets)
            {
                SendStringAsync(x, data);
            }
        }

        private static Task SendStringAsync(WebSocket socket, string data, CancellationToken ct = default(CancellationToken))
        {
            var buffer = Encoding.UTF8.GetBytes(data);
            var segment = new ArraySegment<byte>(buffer);
            return socket.SendAsync(segment, WebSocketMessageType.Text, true, ct);
        }

        private static async Task<string> ReceiveStringAsync(WebSocket socket, CancellationToken ct = default(CancellationToken))
        {
            var buffer = new ArraySegment<byte>(new byte[8192]);
            using (var ms = new MemoryStream())
            {
                WebSocketReceiveResult result;
                do
                {
                    ct.ThrowIfCancellationRequested();

                    result = await socket.ReceiveAsync(buffer, ct);
                    ms.Write(buffer.Array, buffer.Offset, result.Count);
                }
                while (!result.EndOfMessage);

                ms.Seek(0, SeekOrigin.Begin);
                if (result.MessageType != WebSocketMessageType.Text)
                {
                    return null;
                }
              
                using (var reader = new StreamReader(ms, Encoding.UTF8))
                {
                    return await reader.ReadToEndAsync();
                }
            }
        }
    }
}
