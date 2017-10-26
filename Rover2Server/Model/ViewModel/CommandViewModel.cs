using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Rover2Server.Model.ViewModel
{
    public class CommandViewModel
    {
        public string Command { get; set; }
        public string SpeedIncrease { get; set; } = "100";
        public string ServoIncrease { get; set; } = "300";

    }
}
