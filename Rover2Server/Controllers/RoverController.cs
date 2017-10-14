using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Rover2Server.Model.ViewModel;

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
    }
}
