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
      },
      y2: {
        title: {
          display: true,
          text: 'Filtered',
          font: {
            size: 20,
            weight: 'bolder'
          }
        },
        stack: 'app',  // Arbitrary string
        ticks: {
          callback: (value, index, values) => (index == 0 || index == (values.length-1)) ? undefined : value
        }
      },
      y1: {
        title: {
          display: true,
          text: 'Source',
          font: {
            size: 20,
            weight: 'bolder'
          }
        },
        stack: 'app',  // Arbitrary string
        ticks: {
          callback: (value, index, values) => (index == 0 || index == (values.length-1)) ? undefined : value
        }
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


function configureYAxis(axis, values, label) {
  var minValue = 0;
  var maxValue = 0;
  
  for (var j = 0; j < values.length; j++) {
    if (values[j].y < minValue) {
      minValue = values[j].y;
    }
    if (values[j].y > maxValue) {
      maxValue = values[j].y;
    }
  }
  
  chart.options.scales[axis].min = minValue;
  chart.options.scales[axis].max = maxValue;
}


function applyFilter() {
  document.getElementById('lowpass-value').innerHTML = document.getElementById('lowpass').value;
  document.getElementById('highpass-value').innerHTML = document.getElementById('highpass').value / 10;

  axios.post('/filter', {
    'channel' : document.getElementById('channels').value,  // This is the channel index
    'hasLowpass' : document.getElementById('has-lowpass').checked,
    'lowpass' : document.getElementById('lowpass').value,
    'hasHighpass' : document.getElementById('has-highpass').checked,
    'highpass' : document.getElementById('highpass').value / 10
  }).then(function(response) {

    chart.data.datasets = [
      { 
        data: response.data.source,
        borderColor: '#000000',
        fill: false,
        radius: 0,
        borderWidth: 1,
        yAxisID: 'y1'
      },
      { 
        data: response.data.filtered,
        borderColor: '#000000',
        fill: false,
        radius: 0,
        borderWidth: 1,
        yAxisID: 'y2'
      }
    ];

    configureYAxis('y1', response.data.source);
    configureYAxis('y2', response.data.filtered);

    chart.update();
  });
}


BestRendering.InstallFileUploader('input-upload', '/upload', 'data', function(response) {
  channels = BestRendering.ParseJsonFromBackendUpload(response.data);

  document.getElementById('controls').style.display = 'block';
  
  var select = document.getElementById('channels');
  removeOptions(select);

  for (channel in channels) {
    var option = document.createElement('option');
    option.value = channels[channel];  // This is the channel index
    option.text = channel;
    select.add(option);
  }

  applyFilter();
});


document.getElementById('channels').addEventListener('change', applyFilter);
document.getElementById('has-lowpass').addEventListener('change', applyFilter);
document.getElementById('lowpass').addEventListener('change', applyFilter);
document.getElementById('has-highpass').addEventListener('change', applyFilter);
document.getElementById('highpass').addEventListener('change', applyFilter);
