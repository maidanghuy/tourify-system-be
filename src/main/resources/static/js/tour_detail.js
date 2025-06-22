 window.addEventListener("DOMContentLoaded", () => {
        const layers = document.querySelectorAll(".position-absolute");
        layers.forEach((el, index) => {
          setTimeout(() => {
            el.style.transform = `translateX(${index * 10}px)`;
          }, index * 80);
        });
      });

      document.querySelectorAll(".tour-step").forEach((step) => {
        step.addEventListener("click", () => {
          document
            .querySelectorAll(".tour-step")
            .forEach((s) => s.classList.remove("active"));
          step.classList.add("active");
          // Thêm logic scroll/hiện section tương ứng
        });
      });

