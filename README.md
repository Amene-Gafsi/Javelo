# JaVelo Projet

JaVelo is a bicycle route planner for Switzerland, designed to run as a Java application on the user's computer.

JaVelo's interface is similar to online planners like Google Maps. However, JaVelo operates exclusively on the user's computer rather than as a web application.

The image below shows JaVelo's graphical interface, with the route map on the upper part and the elevation profile on the lower part.

![image](https://github.com/user-attachments/assets/7627c0ee-661d-4b90-b975-bbe401a6f562)

Figure 1: Route planning from EPFL to Romont to Fribourg with JaVelo.

The map can be moved, enlarged, or reduced using the mouse. Route planning is done by placing at least two waypoints—the starting point and the destination—by clicking on the map. JaVelo determines the ideal route for a cyclist, considering road types and terrain.

Once a route is calculated, its elevation profile is displayed at the bottom, along with statistics: total length, elevation gains, etc. When the mouse pointer is over a point on the profile, the corresponding point on the route is highlighted on the map, and vice versa. The route can be imported/exported.

Modifying an existing route is possible by adding, deleting, or moving waypoints, triggering the recalculation of the ideal route and its elevation profile.

JaVelo is limited to Switzerland due to the availability of the highly accurate SwissALTI3D elevation model provided by the federal office of topography (swisstopo).
