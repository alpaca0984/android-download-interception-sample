const loadLocalPdf = async (filePath) => {
  const result = await fetch(filePath, {
    headers: {
      "Content-Type": "application/pdf",
    }
  });

  return result.blob();
};

const button = document.querySelector("#button-dl");
button.addEventListener("click", async () => {
  const fileName = "sample.pdf";
  const pdf = await loadLocalPdf(`./${fileName}`);

  const blobStore = new Blob([pdf], {
    type: 'application/pdf'
  });
  const data = window.URL.createObjectURL(blobStore);
  const link = document.createElement('a');
  link.href = data;
  link.download = fileName;
  document.body.appendChild(link);

  link.click();

  // NOTE: When these two lines are activated, Android can not intercept
  // downloading because while it accesses to the blob URL, the URL is
  // already revoked.
  //
  // window.URL.revokeObjectURL(data);
  // link.remove();
});
