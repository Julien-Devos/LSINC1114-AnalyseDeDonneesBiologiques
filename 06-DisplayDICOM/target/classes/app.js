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


var dicomImage = document.createElement('canvas');

function Draw(ctx) {
  if (dicomImage.width > 0 &&
      dicomImage.height > 0) {
    ctx.drawImage(dicomImage, 0, 0);
  }
}

function GetExtent() {
  return BestRendering.CreateExtent(0, 0, dicomImage.width, dicomImage.height);
}

function OnRenderedDicom(response) {
  BestRendering.LoadImageFromBackendIntoCanvas(dicomImage, response.data);
  BestRendering.FitContainer('display');
}

function OnParsedTags(response) {
  var json = BestRendering.ParseJsonFromBackendUpload(response.data);
  document.getElementById('patient-name').innerHTML = json['patient-name'];
  document.getElementById('study-description').innerHTML = json['study-description'];
}

BestRendering.InitializeContainer('display', Draw, GetExtent);
BestRendering.InstallFileUploader('dicom-input', '/render-dicom', 'data', OnRenderedDicom);
BestRendering.InstallFileUploader('dicom-input', '/parse-tags', 'data', OnParsedTags);
