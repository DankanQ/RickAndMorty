package com.dankanq.rickandmorty.utils.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.databinding.FooterLoadStateBinding
import com.dankanq.rickandmorty.databinding.MainLoadStateBinding
import com.dankanq.rickandmorty.utils.presentation.model.LoadError

typealias TryAgainAction = () -> Unit

class MainLoadStateAdapter(
    private val context: Context,
    private val tryAgainAction: TryAgainAction
) : LoadStateAdapter<MainLoadStateAdapter.FooterLoaderViewHolder>() {
    override fun onBindViewHolder(holder: FooterLoaderViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): FooterLoaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FooterLoadStateBinding.inflate(inflater, parent, false)
        return FooterLoaderViewHolder(context, binding, tryAgainAction)
    }

    class FooterLoaderViewHolder(
        private val context: Context,
        private val binding: FooterLoadStateBinding,
        private val refreshAction: TryAgainAction,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.bRetryFooter.setOnClickListener { refreshAction() }
        }

        fun bind(loadState: LoadState) = with(binding) {
            progressBar.isVisible = loadState is LoadState.Loading
            llFooterLoadState.isVisible = loadState is LoadState.Error

            if (loadState is LoadState.Error) {
                val errorMessage = when (val error = loadState.error as LoadError) {
                    is LoadError.NetworkError -> {
                        tvLoadError.text = context.getString(R.string.error_network)
                        bRetryFooter.isVisible = true
                        error.message
                    }
                    is LoadError.HttpError -> {
                        tvLoadError.text = context.getString(R.string.error_http)
                        val errorCode = error.code
                        if (errorCode == 404) {
                            bRetryFooter.isVisible = false
                            context.getString(R.string.error_empty_response)
                        } else {
                            bRetryFooter.isVisible = true
                            context.getString(
                                R.string.error_http_description,
                                error.code.toString()
                            )
                        }
                    }
                    is LoadError.UnknownError -> {
                        tvLoadError.text = context.getString(R.string.error_unknown)
                        bRetryFooter.isVisible = true
                        context.getString(R.string.error_unknown_description)
                    }
                }
                tvLoadErrorInfo.text = errorMessage
            }
        }
    }

    class MainLoaderViewHolder(
        private val context: Context,
        private val binding: MainLoadStateBinding,
        private val swipeRefreshLayout: SwipeRefreshLayout,
        private val refreshAction: TryAgainAction
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.bRetry.setOnClickListener { refreshAction() }
        }

        fun bind(loadState: LoadState) = with(binding) {
            when (loadState) {
                is LoadState.Loading -> {
                    swipeRefreshLayout.isRefreshing = true
                    llMainLoadingState.isVisible = false
                }
                is LoadState.Error -> {
                    swipeRefreshLayout.isRefreshing = false

                    when (val error = loadState.error as LoadError) {
                        is LoadError.NetworkError -> {
                            if (error.hasData) {
                                if (error.characterCount <= 0) {
                                    tvLoadError.text = context.getString(R.string.error_no_data)
                                    tvLoadErrorInfo.text =
                                        context.getString(R.string.error_empty_response)
                                    llMainLoadingState.isVisible = true
                                }
                                llMainLoadingState.isVisible = false
                            } else {
                                tvLoadError.text = context.getString(R.string.error_network)
                                tvLoadErrorInfo.text = error.message
                                bRetry.isVisible = true

                                llMainLoadingState.isVisible = true
                            }
                        }
                        is LoadError.HttpError -> {
                            tvLoadError.text = context.getString(R.string.error_http)
                            val errorCode = error.code
                            if (errorCode == 404) {
                                tvLoadErrorInfo.text =
                                    context.getString(R.string.error_empty_response)
                            } else {
                                tvLoadErrorInfo.text = context.getString(
                                    R.string.error_http_description,
                                    error.code.toString()
                                )
                            }

                            llMainLoadingState.isVisible = true
                        }
                        is LoadError.UnknownError -> {
                            if (error.hasData) {
                                llMainLoadingState.isVisible = false
                            } else {
                                tvLoadError.text = context.getString(R.string.error_unknown)
                                bRetry.isVisible = true
                                tvLoadErrorInfo.text =
                                    context.getString(R.string.error_unknown_description)

                                llMainLoadingState.isVisible = true
                            }
                        }
                    }
                }
                else -> {
                    swipeRefreshLayout.isRefreshing = false
                    llMainLoadingState.isVisible = false
                }
            }
        }
    }
}