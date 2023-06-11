package com.dankanq.rickandmorty.utils.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.data.paging.CharacterRemoteMediator
import com.dankanq.rickandmorty.databinding.FooterLoadStateBinding
import com.dankanq.rickandmorty.databinding.MainLoadStateBinding

typealias TryAgainAction = () -> Unit

class MainLoadStateAdapter(
    private val tryAgainAction: TryAgainAction
) : LoadStateAdapter<RecyclerView.ViewHolder>() {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, loadState: LoadState) {
        when (holder) {
            is FooterLoaderViewHolder -> holder.bind(loadState)
            is MainLoaderViewHolder -> holder.bind(loadState)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (loadState) {
            is LoadState.Loading -> {
                val binding = FooterLoadStateBinding.inflate(inflater, parent, false)
                FooterLoaderViewHolder(binding, tryAgainAction)
            }
            is LoadState.Error -> {
                val binding = FooterLoadStateBinding.inflate(inflater, parent, false)
                FooterLoaderViewHolder(binding, tryAgainAction)
            }
            else -> {
                val binding = MainLoadStateBinding.inflate(inflater, parent, false)
                MainLoaderViewHolder(binding, tryAgainAction)
            }
        }
    }

    class FooterLoaderViewHolder(
        private val binding: FooterLoadStateBinding,
        private val tryAgainAction: TryAgainAction
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.bRetryFooter.setOnClickListener { tryAgainAction() }
        }

        fun bind(loadState: LoadState) =
            with(binding) {
                progressBar.isVisible = loadState is LoadState.Loading
                tvLoadError.isVisible = loadState is LoadState.Error
                tvLoadErrorInfo.isVisible = loadState is LoadState.Error
                bRetryFooter.isVisible = loadState is LoadState.Error
            }
    }

    class MainLoaderViewHolder(
        private val binding: MainLoadStateBinding,
        private val tryAgainAction: TryAgainAction
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.bRetry.setOnClickListener { tryAgainAction() }
        }

        // вынести в строковые ресурсы
        fun bind(loadState: LoadState) = with(binding) {
            when (loadState) {
                is LoadState.Error -> {
                    when (val error = loadState.error) {
                        is CharacterRemoteMediator.LoadError.NetworkError -> {
                            val errorText = if (error.hasData) {
                                llMainLoadingState.isVisible = false
                                "Network error (loaded local data)"
                            } else {
                                llMainLoadingState.isVisible = true
                                "Network error"
                            }
                            tvLoadError.text = errorText
                            tvLoadErrorInfo.text = error.message
                        }
                        is CharacterRemoteMediator.LoadError.HttpError -> {
                            tvLoadError.text = "HTTP error"
                            tvLoadErrorInfo.text = "Code: ${error.code}"
                            llMainLoadingState.isVisible = true
                        }
                        else -> {
                            tvLoadError.text = "Undefined error"
                            tvLoadErrorInfo.text = "Undefined error description"
                            llMainLoadingState.isVisible = true
                        }
                    }
                }
                else -> {
                    progressBar.isVisible = loadState is LoadState.Loading
                }
            }
        }
    }
}