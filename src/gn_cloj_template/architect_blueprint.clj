(ns gn-cloj-template.arch-blueprint
  (:use clj-pdf.core))

(def a1-size [2384 1684])
(def a1-wireframe-stroke 5)
(def a1-title-stroke 3)
(def a1-cell-size 54)
(def a1-cell-stroke 1)

(defn calc-a1-content-rect
  []
  (let [width-content (- (first a1-size) (* 2 a1-wireframe-stroke) 10)
        height-content (- (second a1-size) (* 2 a1-wireframe-stroke) 10)
        count-x (Math/floor
                 (/ (- width-content a1-cell-stroke)
                    (+ a1-cell-size a1-cell-stroke)))
        count-y (Math/floor
                 (/ (- height-content a1-cell-stroke)
                    (+ a1-cell-size a1-cell-stroke)))
        actual-width (+ (* count-x a1-cell-size) (* (dec count-x) a1-cell-stroke))
        actual-height (+ (* count-y a1-cell-size) (* (dec count-y) a1-cell-stroke))
        x-inset (-> (- (first a1-size) actual-width)
                    (/ 2)
                    (Math/floor))
        y-inset (-> (- (second a1-size) actual-height)
                    (/ 2)
                    (Math/floor))]
    [x-inset y-inset actual-width actual-height]))

(defn inset-rect
  [rect inset-x inset-y]
  (let [[x y w h] rect]
    [(+ x inset-x)
     (+ y inset-y)
     (- w (* 2 inset-x))
     (- h (* 2 inset-y))]))

(defn max-x
  [rect]
  (let [[x y w h] rect]
    (+ x w)))

(defn max-y
  [rect]
  (let [[x y w h] rect]
    (+ y h)))

(def a1-content-rect (calc-a1-content-rect))
(def a1-wireframe-rect (inset-rect a1-content-rect
                                   (* -1 a1-wireframe-stroke)
                                   (* -1 a1-wireframe-stroke)))


(defn draw-rect
  [g2d rect stroke]
  (let [[x y w h] rect]
    (doto g2d
      (.fillRect x y w stroke)
      (.fillRect x y stroke h)
      (.fillRect x (- (+ h y) stroke) w stroke)
      (.fillRect (- (+ w x) stroke) y stroke h))))

(defn fill-rect
  [g2d rect]
  (let [[x y w h] rect]
    (.fillRect g2d x y w h)))

(defn draw-bg
  [g2d]
  (doto g2d
    (.setColor (java.awt.Color. 255 255 255))
    (.fillRect 0 0 (first a1-size) (second a1-size))))

(defn draw-wireframe
  [g2d]
  (doto g2d
    (.setColor (java.awt.Color. 0 0 0))
    (draw-rect a1-wireframe-rect a1-wireframe-stroke)))

(defn draw-grid
  [g2d frame cell-size stroke]
  (let [[x y w h] frame]
    (loop [ix x]
      (if (>= ix (+ x w))
        0
        (do
          (.fillRect g2d ix y stroke h)
          (recur (+ ix cell-size stroke)))))
    (loop [iy y]
      (if (>= iy (+ y h))
        0
        (do
          (.fillRect g2d x iy w stroke)
          (recur (+ iy cell-size stroke)))))))


(defn draw-title
  [g2d]
    (let [a1-title-size [(+ (* 13 a1-cell-size) (* 12 a1-cell-stroke))
                         (+ (* 6 a1-cell-size) (* 4 a1-cell-stroke))]
          a1-title-rect [(- (max-x a1-content-rect) (first a1-title-size))
                         (- (max-y a1-content-rect) (second a1-title-size))
                         (+ (first a1-title-size) a1-wireframe-stroke)
                         (+ (second a1-title-size) a1-wireframe-stroke)]
          a1-midline-size [(+ (* 9 a1-cell-size) (* 8 a1-cell-stroke))
                           (+ (* 6 a1-cell-size) (* 4 a1-cell-stroke))]
          a1-midline-rect [(- (max-x a1-content-rect) (first a1-midline-size))
                           (- (max-y a1-content-rect) (second a1-midline-size))
                           a1-title-stroke
                           (second a1-title-size)]
          a1-horizontal-rect-1 [(- (max-x a1-content-rect) (first a1-title-size))
                                (- (max-y a1-content-rect)
                                   (+ (* 4 a1-cell-size) (* 3 a1-cell-stroke)))
                                (+ (* 4 a1-cell-size) (* 3 a1-cell-stroke))
                                a1-title-stroke]
          a1-horizontal-rect-2 [(- (max-x a1-content-rect) (first a1-title-size))
                                (- (max-y a1-content-rect)
                                   (+ (* 2 a1-cell-size) (* 1 a1-cell-stroke)))
                                (+ (* 4 a1-cell-size) (* 3 a1-cell-stroke))
                                a1-title-stroke]]
      (doto g2d
        (.setColor java.awt.Color/BLACK)
        (draw-rect a1-title-rect a1-title-stroke)
        (fill-rect a1-midline-rect)
        (fill-rect a1-horizontal-rect-1)
        (fill-rect a1-horizontal-rect-2))))

(pdf
 [{:size :a1
   :orientation :landscape}
  [:graphics {} #(draw-bg %)]
  [:graphics {} #(draw-wireframe %)]
  [:graphics {} (fn [g2d]
                  (.setColor g2d (java.awt.Color. 0 0 0 128))
                  (draw-grid g2d a1-content-rect 10 a1-cell-stroke))]
  [:graphics {} (fn [g2d]
                  (.setColor g2d java.awt.Color/BLACK)
                  (draw-grid g2d a1-content-rect a1-cell-size a1-cell-stroke))]
  [:graphics {} #(draw-title %)]
  ]
 "doc.pdf")
