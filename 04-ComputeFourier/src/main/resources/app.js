/**
 * Copyright (c) 2022, Sebastien Jodogne, ICTEAM UCLouvain, Belgium
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/


var chart = new Chart(document.getElementById('myChart'), {
  type: 'line',
  data: {
    datasets: [ ]
  },
  options: {
    interaction: {
      mode: 'nearest',
      intersect: false,
      axis: 'x'
    },
    //events: [],  // Disable hover
    animation: false,
    maintainAspectRatio: false,
    title: {
      display: true,
      text: 'Channel'
    },
    scales: {
      x: {
        type: 'linear'
      }
    },
    plugins: {
      legend: {
        display: false
      },
      zoom: {
        pan: {
          enabled: true,
          mode: 'x'
        },
        zoom: {
          wheel: {
            enabled: true,
          },
          pinch: {
            enabled: true
          },
          mode: 'x'
        }
      }
    }
  }
});


// https://stackoverflow.com/questions/3364493/how-do-i-clear-all-options-in-a-dropdown-box
function removeOptions(selectElement) {
   var L = selectElement.options.length - 1;
   for (var i = L; i >= 0; i--) {
      selectElement.remove(i);
   }
}


function computeSpectrum() {
  var select = document.getElementById('channels');

  axios.post('/compute-power-spectrum', {
    'channel' : select.value  // This is the channel index
  }).then(function(response) {
    var minValue = 0;
    var maxValue = 0;

    // NB: We begin at "j=1" to ignore peaks due to the DC component
    for (var j = 1; j < response.data.length; j++) {
      if (response.data[j].y < minValue) {
        minValue = response.data[j].y;
      }
      if (response.data[j].y > maxValue) {
        maxValue = response.data[j].y;
      }
    }

    chart.data.datasets = [ { 
      data: response.data,
      borderColor: '#000000',
      //label: channelName,
      fill: false,
      radius: 0,
      borderWidth: 1
    } ];
    
    chart.options.scales['y'] = {
        title: {
          display: false
        },
        type: 'linear',
        ticks: {
          callback: (value, index, values) => (index == 0 || index == (values.length-1)) ? undefined : value
        },
        min: minValue,
        max: maxValue
    };

    chart.update();
  });
}


BestRendering.InstallFileUploader('input-upload', '/upload', 'data', function(response) {
  channels = BestRendering.ParseJsonFromBackendUpload(response.data);

  var select = document.getElementById('channels');
  removeOptions(select);

  for (channel in channels) {
    var option = document.createElement('option');
    option.value = channels[channel];  // This is the channel index
    option.text = channel;
    select.add(option);
  }

  computeSpectrum();
});


document.getElementById('channels').addEventListener('change', computeSpectrum);
