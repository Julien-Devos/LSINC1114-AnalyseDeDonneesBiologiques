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


function DrawImage(ctx, image) {
  if (image.width > 0 &&
      image.height > 0) {
    BestRendering.DisableAntialiasing(ctx);
    ctx.drawImage(image, 0, 0);
  }
}

function GetImageExtent(image) {
  return BestRendering.CreateExtent(0, 0, image.width, image.height);
}


var topLeftImage = document.createElement('canvas');
var topRightImage = document.createElement('canvas');
var bottomLeftImage = document.createElement('canvas');
var bottomRightImage = document.createElement('canvas');

BestRendering.InitializeContainer('topLeft',
                                  function(ctx) { DrawImage(ctx, topLeftImage) },
                                  function() { return GetImageExtent(topLeftImage) });
BestRendering.InitializeContainer('topRight',
                                  function(ctx) { DrawImage(ctx, topRightImage) },
                                  function() { return GetImageExtent(topRightImage) });
BestRendering.InitializeContainer('bottomLeft',
                                  function(ctx) { DrawImage(ctx, bottomLeftImage) },
                                  function() { return GetImageExtent(bottomLeftImage) });
BestRendering.InitializeContainer('bottomRight',
                                  function(ctx) { DrawImage(ctx, bottomRightImage) },
                                  function() { return GetImageExtent(bottomRightImage) });


function OnUploadedSource(response) {
  axios.get('render-source', {
    responseType: 'arraybuffer'
  }).then(function (response) {
    BestRendering.LoadImageFromBackendIntoCanvas(topLeftImage, response.data);
    BestRendering.FitContainer('topLeft');
  });

  axios.get('render-sobel-x', {
    responseType: 'arraybuffer'
  }).then(function (response) {
    BestRendering.LoadImageFromBackendIntoCanvas(bottomLeftImage, response.data);
    BestRendering.FitContainer('bottomLeft');
  });

  axios.get('render-sobel-y', {
    responseType: 'arraybuffer'
  }).then(function (response) {
    BestRendering.LoadImageFromBackendIntoCanvas(bottomRightImage, response.data);
    BestRendering.FitContainer('bottomRight');
  });

  axios.get('render-sobel-magnitude', {
    responseType: 'arraybuffer'
  }).then(function (response) {
    BestRendering.LoadImageFromBackendIntoCanvas(topRightImage, response.data);
    BestRendering.FitContainer('topRight');
  });
}

BestRendering.InstallFileUploader('dicom-input', '/upload-source', 'data', OnUploadedSource);
