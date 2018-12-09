# How to build the docker image

```make build```

# How to publish the docker image

```make publish```

# Run the container

``run.sh```

# Run within Docker Compose

```
zkuiboot:
  image: zkuiboot
  ports:
    - "9090:9090"
```
