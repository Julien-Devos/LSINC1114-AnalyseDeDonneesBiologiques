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
    events: [],  // Disable hover
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


function addSamplesToChart(plotIndex, data, color) {
  var minValue = 0;
  var maxValue = 0;
  for (var i = 0; i < data.length; i++) {
    if (data[i].y < minValue) {
      minValue = data[i].y;
    }
    if (data[i].y > maxValue) {
      maxValue = data[i].y;
    }
  }
  
  chart.data.datasets[plotIndex].data = data;

  var yAxisID = chart.data.datasets[plotIndex].yAxisID;
  chart.options.scales[yAxisID].min = minValue;
  chart.options.scales[yAxisID].max = maxValue;
  
  chart.update();
}


function updateChart() {
  // Firstly, clear the content of the chart
  chart.data.datasets = [];
  chart.options.scales = {
    x: {
      type: 'linear'
    }
  };

  // Secondly, retrieve the selected channels the checkboxes
  var checkboxes = document.getElementsByClassName('checkboxes');
  var plotIndex = 0;
  for (var i = 0; i < checkboxes.length; i++) {
    if (checkboxes[i].checked) {
      var channelIndex = checkboxes[i].channelIndex;
      var channelName = checkboxes[i].channelName;
      var yAxisID = 'y' + plotIndex;

      chart.data.datasets.push({ 
        data: [],
        borderColor: '#000000',
        label: channelName,
        fill: false,
        radius: 0,
        borderWidth: 1,
        yAxisID: yAxisID
      });

      chart.options.scales[yAxisID] = {
        title: {
          display: true,
          text: channelName,
          font: {
            size: 20,
            weight: 'bolder'
          }
        },
        type: 'linear',
        stack: 'app',  // Arbitrary string
        ticks: {
          callback: (value, index, values) => (index == 0 || index == (values.length-1)) ? undefined : value
        }
      };

      axios.get('samples?channel=' + channelIndex).then(function(plotIndex) {
        return function(response) {
          addSamplesToChart(plotIndex, response.data, '#000000');
        }
      } (plotIndex));

      plotIndex++;
    }
  }
  
  chart.update();
}


BestRendering.InstallFileUploader('input-upload', '/upload', 'data', function(response) {
  axios.get('channels').then(function(channels) {
    var target = document.getElementById('channels');
    target.innerHTML = '';
    
    for (var name in channels.data) {
      var input = document.createElement('input');
      input.type = 'checkbox';
      input.channelIndex = channels.data[name];
      input.channelName = name;
      input.className = 'checkboxes';

      var span = document.createElement('span');
      span.innerHTML = name;

      input.addEventListener('change', updateChart);
      target.appendChild(input);
      target.appendChild(span);
    }

    updateChart(); // Clear the drawing
  });
});
