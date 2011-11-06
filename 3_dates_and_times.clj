;; @@PLEAC@@_3.0 Dates and Times

;; @@PLEAC@@_3.1 Introduction
;;------------------------------------
;; Use a calendar to compute year, month, day, hour, minute and second values.

(import '(java.util Calendar))
(import '(java.util GregorianCalendar))

(defn print-day-of-year []
  (let [cal (GregorianCalendar.)]
    (printf "Today is day %d of the current year.\n"
            (.get cal Calendar/DAY_OF_YEAR))))

;; @@PLEAC_3.2 Finding Today's Date
;;------------------------------------
(import '(java.util Calendar))
(import '(java.util GregorianCalendar))

(defn todays-date []
  (let [cal (GregorianCalendar.)
        day (.get cal Calendar/DATE)
        month (.get cal Calendar/MONTH)
        year (.get cal Calendar/YEAR)]
    [day month year]))

(defn print-todays-date []
  (let [[day month year] (todays-date)]
    (printf "The current date is %d %02d %02d\n" year (inc month) day)))

;; @@PLEAC_3.3 Converting DMYHMS to Epoch Seconds
;;------------------------------------
(import '(java.util Calendar))
(import '(java.util TimeZone))
(import '(java.util GregorianCalendar))

(defn epoch-seconds-of-dmyhms [tz day month year hour minute second]
  (let [cal (GregorianCalendar.)
        zone (TimeZone/getTimeZone tz)]
    (do
      (.setTimeZone cal zone)
      (.set cal Calendar/DAY_OF_MONTH day)
      (.set cal Calendar/MONTH month)
      (.set cal Calendar/YEAR year)
      (.set cal Calendar/HOUR_OF_DAY hour)
      (.set cal Calendar/MINUTE minute)
      (.set cal Calendar/SECOND second)
      (int (/ (.getTime (.getTime cal)) 1000)))))

(epoch-seconds-of-dmyhms "UTC" 4 10 2011 12 30 55)

;; @@PLEAC_3.4 Converting Epoch Seconds to DMYHMS
;;------------------------------------
(import '(java.util Calendar))
(import '(java.util Date))
(import '(java.util GregorianCalendar))

(defn dmyhms-of-epoch-seconds [seconds]
  (let [cal (GregorianCalendar.)
        date (Date. (* seconds 1000))]
    (do
      (.setTime cal date)
      [(.get cal Calendar/DAY_OF_MONTH)
       (.get cal Calendar/MONTH)
       (.get cal Calendar/YEAR)
       (.get cal Calendar/HOUR_OF_DAY)
       (.get cal Calendar/MINUTE)
       (.get cal Calendar/SECOND)])))

(defn print-dmyhms-of-epoch-seconds [seconds]
  (let [[day month year hour minute seconds] (dmyhms-of-epoch-seconds seconds)]
    (printf "%02d-%02d-%d %02d:%02d:%02d\n"
            day (inc month) year hour minute seconds)))

;; @@PLEAC_3.5 Adding or Subtracting from a Date
