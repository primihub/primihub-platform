from django.urls import path

from . import views

urlpatterns = [
    path('run', views.run, name='run'),
]
