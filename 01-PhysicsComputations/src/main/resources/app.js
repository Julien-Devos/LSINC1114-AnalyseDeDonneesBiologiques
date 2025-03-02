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


var ELECTRICITY = [ 'voltage', 'resistance', 'current', 'power' ];


document.getElementById('convert-celsius').addEventListener('click', function() {
  var body = {
    celsius: document.getElementById('celsius').value
  };
  
  axios.post('convert-celsius', body).then(function(response) {
    var answer = response.data;
    document.getElementById('fahrenheit').innerHTML = answer.fahrenheit;
    document.getElementById('kelvin').innerHTML = answer.kelvin;
  }).catch(function(error) {
    alert('Error during conversion!');
  });
});


document.getElementById('compute-electricity').addEventListener('click', function() {
  var body = {};

  for (var i = 0; i < ELECTRICITY.length; i++) {
    var value = document.getElementById(ELECTRICITY[i]).value;
    if (value !== '') {
      body[ELECTRICITY[i]] = value;
    }
  }
  
  axios.post('compute-electricity', body).then(function(response) {
    var answer = response.data;
    
    for (var i = 0; i < ELECTRICITY.length; i++) {
      if (ELECTRICITY[i] in answer) {
        document.getElementById(ELECTRICITY[i]).value = answer[ELECTRICITY[i]];
      }
    }
  }).catch(function(error) {
    alert('Error during computations!');
  });
});


document.getElementById('clear-electricity').addEventListener('click', function() {
  for (var i = 0; i < ELECTRICITY.length; i++) {
    document.getElementById(ELECTRICITY[i]).value = '';
  }
});
