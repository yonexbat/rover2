using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Rover2Server.Model.ViewModel;
using Microsoft.AspNetCore.Http;
using System.IO;

// For more information on enabling MVC for empty projects, visit https://go.microsoft.com/fwlink/?LinkID=397860

namespace Rover2Server.Controllers

{
    public class RoverController : Controller    {      

        public IActionResult Index(CommandViewModel vm)
        {
            if(!string.IsNullOrWhiteSpace(vm.Command))
            {
                Commander.SendString(vm.Command);
            }
            return View(vm);
        }

        [HttpPost]
        [HttpGet]
        public async Task<IActionResult> UploadImage(ICollection<IFormFile> bitmap)
        {


            using (MemoryStream memoryStream = new MemoryStream())
            {

                IFormFile x = bitmap.First();
                await x.CopyToAsync(memoryStream);
                memoryStream.Flush();
                Commander.SetImage(memoryStream.ToArray());

            }

            return Content("Ok");
        }

        public async Task<IActionResult> Image()
        {
            byte[] image = Commander.GetImage();
            return File(image, "image/jpeg");
        }
    }
}
