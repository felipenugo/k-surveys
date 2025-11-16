# Explicación de Métricas de Calidad de Clustering

Este documento explica el significado e interpretación de las tres principales métricas de calidad de clustering utilizadas en el análisis: **Silhouette Score**, **Calinski-Harabasz Score** y **Davies-Bouldin Score**.

---

## 1. Silhouette Score

* **Rango**: -1 a 1  
* **Interpretación**: El Silhouette Score mide cuán similar es un objeto a su propio clúster (cohesión) en comparación con otros clústeres (separación). Ayuda a evaluar la calidad del clustering.

  * **Valores cercanos a +1**: Indican que el objeto está bien asignado a su clúster y mal asignado a los clústeres vecinos. Esto sugiere una estructura de clustering fuerte, donde los clústeres son densos y están bien separados.
  * **Valores cercanos a 0**: Indican que el objeto está sobre o muy cerca del límite de decisión entre dos clústeres vecinos. Esto sugiere clústeres solapados o que el objeto podría pertenecer a cualquiera de los dos.
  * **Valores cercanos a -1**: Indican que probablemente el objeto está asignado al clúster incorrecto, ya que es más similar a un clúster vecino que al suyo propio.

* **Objetivo**: **Cuanto más alto, mejor**. Generalmente se busca un Silhouette Score cercano a 1.

---

## 2. Calinski-Harabasz Score (Criterio de Razón de Varianza)

* **Rango**: 0 a ∞ (infinito)  
* **Interpretación**: El Calinski-Harabasz Score se define como la relación entre la dispersión media entre clústeres y la dispersión dentro de los clústeres. En términos simples, mide cuánta varianza hay entre los clústeres frente a cuánta varianza hay dentro de cada clúster.

  * **Valores altos**: Generalmente indican clústeres bien definidos. Esto significa que los clústeres son densos y están bien separados entre sí.
  * **Valores bajos**: Sugieren que los clústeres no son muy distintos o están demasiado dispersos.

* **Objetivo**: **Cuanto más alto, mejor**. No hay un valor “bueno” absoluto; se utiliza principalmente para comparar diferentes resultados de clustering en el mismo conjunto de datos, con el fin de encontrar el número óptimo de clústeres o el mejor algoritmo.

---

## 3. Davies-Bouldin Score

* **Rango**: 0 a ∞ (infinito)  
* **Interpretación**: El Davies-Bouldin Score mide la “similaridad” promedio entre cada clúster y su clúster más similar. La similaridad se define como la relación entre las distancias internas del clúster y las distancias entre clústeres.

  * **Valores cercanos a 0**: Indican una mejor partición. Esto significa que los clústeres son compactos (baja distancia interna) y están bien separados (alta distancia entre clústeres).
  * **Valores altos**: Indican un peor clustering, donde los clústeres no son suficientemente compactos o no están bien separados.

* **Objetivo**: **Cuanto más bajo, mejor**. Generalmente se busca un Davies-Bouldin Score cercano a 0.

---
