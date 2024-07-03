document.querySelectorAll(".drop-zone__input").forEach((inputElement) => {
    const dropZoneElement = inputElement.closest(".drop-zone");

    dropZoneElement.addEventListener("click", (e) => {
        inputElement.click();
    });

    inputElement.addEventListener("change", (e) => {
        if (inputElement.files.length) {
            updateThumbnails(dropZoneElement, inputElement.files);
        }
    });

    dropZoneElement.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropZoneElement.classList.add("drop-zone--over");
    });

    ["dragleave", "dragend"].forEach((type) => {
        dropZoneElement.addEventListener(type, (e) => {
            dropZoneElement.classList.remove("drop-zone--over");
        });
    });

    dropZoneElement.addEventListener("drop", async (e) => {
        e.preventDefault();
        dropZoneElement.classList.remove("drop-zone--over");

        const items = e.dataTransfer.items;
        if (items && items.length) {
            const filesWithPaths = await getAllFilesWithPathsFromItems(items);
            if (filesWithPaths.length) {
                updateThumbnails(dropZoneElement, filesWithPaths.map(f => f.file));
                const formData = new FormData();
                filesWithPaths.forEach(({ file, path }) => {
                    formData.append('files', file, path);
                });

                // Add action field to formData
                const action = inputElement.closest('form').querySelector('input[name="action"]').value;
                formData.append('action', action);

                // Debug: Check the formData content
                for (const [key, value] of formData.entries()) {
                    console.log(`${key}: ${value.name || value}`);
                }

                submitFormData(formData);
            }
        }
    });
});

async function getAllFilesWithPathsFromItems(items) {
    let filesWithPaths = [];
    for (const item of items) {
        const entry = item.webkitGetAsEntry();
        if (entry.isDirectory) {
            filesWithPaths = filesWithPaths.concat(await getFilesWithPathsFromDirectory(entry, entry.fullPath));
        } else {
            filesWithPaths.push({ file: item.getAsFile(), path: entry.fullPath });
        }
    }
    return filesWithPaths;
}

function getFilesWithPathsFromDirectory(directory, basePath) {
    return new Promise((resolve) => {
        const dirReader = directory.createReader();
        const allFiles = [];

        const readEntries = () => {
            dirReader.readEntries(async (entries) => {
                if (entries.length === 0) {
                    resolve(allFiles);
                } else {
                    for (const entry of entries) {
                        if (entry.isDirectory) {
                            const nestedFiles = await getFilesWithPathsFromDirectory(entry, basePath);
                            allFiles.push(...nestedFiles);
                        } else {
                            const file = await getFile(entry);
                            allFiles.push({ file, path: entry.fullPath });
                        }
                    }
                    readEntries();
                }
            });
        };

        readEntries();
    });
}

function getFile(fileEntry) {
    return new Promise((resolve) => fileEntry.file(resolve));
}

function updateThumbnails(dropZoneElement, files) {
    // Remove any existing thumbnails
    const existingThumbnails = dropZoneElement.querySelectorAll(".drop-zone__thumb");
    existingThumbnails.forEach(thumbnail => thumbnail.remove());

    // Remove the prompt
    if (dropZoneElement.querySelector(".drop-zone__prompt")) {
        dropZoneElement.querySelector(".drop-zone__prompt").remove();
    }

    // Create a thumbnail for each file
    Array.from(files).forEach(file => {
        let thumbnailElement = document.createElement("div");
        thumbnailElement.classList.add("drop-zone__thumb");
        thumbnailElement.dataset.label = file.name;
        dropZoneElement.appendChild(thumbnailElement);

        // Show thumbnail for image files
        if (file.type.startsWith("image/")) {
            const reader = new FileReader();

            reader.readAsDataURL(file);
            reader.onload = () => {
                thumbnailElement.style.backgroundImage = `url('${reader.result}')`;
            };
        } else {
            thumbnailElement.style.backgroundImage = null;
        }
    });
}

function submitFormData(formData) {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', '/add-files', true);
    xhr.onload = function () {
        if (xhr.status === 200) {
            window.location.href = '/main-page';
        } else {
            console.error('An error occurred!');
        }
    };
    xhr.send(formData);
}
