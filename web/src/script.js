const button = document.querySelector("#button-dl");
button.addEventListener("click", function (e) {
  const blobStore = new Blob(["Hello-World"], {
    type: 'text/plain'
  });
  const data = window.URL.createObjectURL(blobStore);
  const link = document.createElement('a');
  link.href = data;
  link.download = 'sample.txt';
  document.body.appendChild(link);

  link.click();

  window.URL.revokeObjectURL(data);
  link.remove();
});
